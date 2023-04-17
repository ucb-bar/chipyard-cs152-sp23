package chipyard

import chisel3._

import freechips.rocketchip.config.{Config, Parameters}
import freechips.rocketchip.rocket._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.tile.{RocketTileParams}

/**********************************************************************
 * Lab 5
 **********************************************************************/

class WithNLab5Cores(n: Int) extends Config((site, here, up) => {
  case RocketTilesKey => {
    val lab5 = RocketTileParams(
      core   = RocketCoreParams(mulDiv = Some(MulDivParams(
        mulUnroll = 8,
        mulEarlyOut = true,
        divEarlyOut = true))),
      dcache = Some(DCacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 16,
        nWays = 4,
        nTLBSets =  1,
        nTLBWays = 4,
        nMSHRs = 0,
        blockBytes = site(CacheBlockBytes))),
      icache = Some(ICacheParams(
        rowBits = site(SystemBusKey).beatBits,
        nSets = 64,
        nWays = 4,
        nTLBSets = 1,
        nTLBWays = 4,
        blockBytes = site(CacheBlockBytes))))
    List.tabulate(n)(i => lab5.copy(hartId = i))
  }
})


class Lab5RocketConfig extends Config(
  new chipyard.harness.WithUARTAdapter ++                      // display UART with a SimUARTAdapter
  new chipyard.harness.WithTieOffInterrupts ++                 // tie off top-level interrupts
  new chipyard.harness.WithBlackBoxSimMem ++                   // drive the master AXI4 memory with a SimAXIMem
  new chipyard.harness.WithTiedOffDebug ++                     // tie off debug (since we are using SimSerial for testing)
  new chipyard.harness.WithSimSerial ++                        // drive TSI with SimSerial for testing
  // new testchipip.WithTSI ++                                      // use testchipip serial offchip link
  // new chipyard.config.WithNoGPIO ++                              // no top-level GPIO pins (overrides default set in sifive-blocks)
  new chipyard.config.WithBootROM ++                             // use default bootrom
  new chipyard.config.WithUART ++                                // add a UART
  new freechips.rocketchip.subsystem.WithNoMMIOPort ++           // no top-level MMIO master port (overrides default set in rocketchip)
  new freechips.rocketchip.subsystem.WithNoSlavePort ++          // no top-level MMIO slave port (overrides default set in rocketchip)
  new freechips.rocketchip.subsystem.WithNExtTopInterrupts(0) ++ // no external interrupts
  new WithNLab5Cores(1) ++                                       // single rocket core
  new freechips.rocketchip.system.BaseConfig)                    // "base" rocketchip system

class Lab5MESIDualRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithMESICoherence ++
  new WithNLab5Cores(2) ++                                       // dual rocket cores
  new Lab5RocketConfig)

class Lab5MSIDualRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithMSICoherence ++
  new WithNLab5Cores(2) ++                                       // dual rocket cores
  new Lab5RocketConfig)

class Lab5MIDualRocketConfig extends Config(
  new freechips.rocketchip.subsystem.WithMICoherence ++
  new WithNLab5Cores(2) ++                                       // dual rocket cores
  new Lab5RocketConfig)
