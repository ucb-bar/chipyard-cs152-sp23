#!/bin/bash

bash << "EOF"
source ./env.sh
export PATH="/home/ff/cs152/sp22/lab1-install/bin:$PATH"
cd /scratch/${USER}/chipyard-cs152-sp23/generators/riscv-sodor/test/custom-bmarks
make
make run
make dump
EOF
