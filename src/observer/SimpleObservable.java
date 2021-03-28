package observer;

public class SimpleObservable<T> implements Observable<T> {
    private boolean changed = false;
    private List<Observer<T>> observers;

    public SimpleObservable(){
        observers = new CopyOnWriteArrayList<>();
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void deleteObserver(Observer o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        notifyObservers(null);
    }

    @Override
    public void notifyObservers(T arg) {
        if(!changed)
            return;
        clearChanged();
        for (Observer o: observers) {
            o.update(this,arg);
        }
    }

    @Override
    public void deleteObservers() {
        observers.clear();
    }

    @Override
    public void setChanged() {
        changed = true;
    }

    @Override
    public void clearChanged() {
        changed = false;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }

    @Override
    public int countObservers() {
        return observers.size();
    }
}
