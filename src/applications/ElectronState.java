package applications;

interface ElectronState {

  boolean isGround();
}

@SuppressWarnings("CheckStyle")
class Ground implements ElectronState {

  @Override
  public boolean isGround() {
    return true;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}

@SuppressWarnings("CheckStyle")
class Excited implements ElectronState {

  @Override
  public boolean isGround() {
    return false;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}