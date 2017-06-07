
export default class Observable {
  subscribers = [];
  previous = null;
  subscribe(fn) {
    this.subscribers.push(fn);
  }
  set(value) {
    if (value !== this.previous) {
      this.subscribers.forEach(s => s(value));
    }
    this.previous = value;
  }
  get() {
    return this.previous;
  }
}
