package base.interfaces;

public interface Context {

    void registerListener(Listener listener);
    void notifyListeners();
}
