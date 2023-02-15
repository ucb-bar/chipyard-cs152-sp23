#!/bin/bash

set -x

function retry {
    command="$*"
    retval=1
    attempt=1
    until [[ $retval -eq 0 ]] || [[ $attempt -gt 5 ]]; do
        # Execute inside of a subshell in case parent
        # script is running with "set -e"
        (
            set +e
            $command
        )
        retval=$?
        attempt=$(( $attempt + 1 ))
        if [[ $retval -ne 0 ]]; then
            # If there was an error wait 10 seconds
            sleep 10
        fi
    done
    if [[ $retval -ne 0 ]] && [[ $attempt -gt 5 ]]; then
        # Something is fubar, go ahead and exit
        exit $retval
    fi
}

export TESTDIR=${LAB2ROOT}/lab/directed
cd ${TESTDIR}
retry make

cd ${SIMDIR}
retry make CONFIG=CS152RocketConfig -j4

retry make CONFIG=CS152RocketConfig run-binary-hex BINARY=${TESTDIR}/transpose.riscv

retry spike ${TESTDIR}/transpose.riscv

retry make CONFIG=CS152RocketL2Config run-binary-hex BINARY=${TESTDIR}/transpose.riscv

cd ${LAB2ROOT}/lab/open1
retry make sim -j4
retry make ccbench-sweep -j4

retry make ccbench-plot

export TESTDIR=${LAB2ROOT}/lab/open1/test
cd ${TESTDIR}
retry make

cd ${SIMDIR}
retry make CONFIG=CS152RocketMysteryConfig run-binary-hex BINARY="${TESTDIR}/bmark-p.riscv"

retry make CONFIG=CS152RocketMysteryConfig run-pk PAYLOAD="${TESTDIR}/bmark-v.riscv"

retry make CONFIG=CS152RocketPrefetchConfig -j4

retry make CONFIG=CS152RocketPrefetchConfig run-binary-hex BINARY="${LAB2ROOT}/lab/directed/transpose.riscv"

retry make CONFIG=CS152RocketPrefetchConfig run-bmark-tests

cd ${LAB2ROOT}/lab/open2
retry make
cd ${SIMDIR}
retry make CONFIG=CS152RocketPrefetchConfig run-bfs

retry make CONFIG=CS152RocketPrefetchConfig run-binary-debug-hex BINARY="${LAB2ROOT}/lab/directed/transpose.riscv"
retry make CONFIG=CS152RocketPrefetchConfig run-bmark-tests-debug
retry make CONFIG=CS152RocketPrefetchConfig run-bfs-debug

cd ${LAB2ROOT}/lab/open3
retry make spike -j4
retry make run

retry make clean-run

retry make clean

cd ${LAB2ROOT}/lab/open3
retry make spike
