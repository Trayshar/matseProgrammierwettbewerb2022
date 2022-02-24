package tooling;

import java.util.TimerTask;

public class Observer extends TimerTask {

    private final IObservable o;
    public Observer(IObservable o) {
        this.o = o;
    }

    @Override
    public void run() {
        System.out.println(o.get1Second());
    }

    public interface IObservable {
        String get1Second();
    }
}
