package pincet;

import pincet.model.VmGcData;
import pincet.model.VmStat;

import java.util.concurrent.Future;

@FunctionalInterface
public interface VmDataHandler {
  default Future<VmStat> stat() {
    return Pincet.context().stat();
  }

  VmGcData handle(VmGcData gcData);
}
