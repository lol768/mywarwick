import Rx from 'rx';
import log from 'loglevel';

/**
 * Simple binary backoff for retrying failed connections.
 *
 * When you connect, call backoff.success().
 * If you get disconnected, call backoff.retry(reconnect) with
 * your choice of reconnect function.
 */
class BinaryBackoff {
  constructor(min, max, reset) {
    this.min = min;
    this.max = max;
    this.reset = reset;
    this.current = min;

    this.retries = new Rx.Subject();
  }

  /** On failure, do the given callback function some time later. */
  retry(fn) {
    clearTimeout(this.resetTimer);
    this.timer = setTimeout(fn, this.current);
    this.retries.onNext(this.current);
    this.current = Math.min(this.current * 2, this.max);
  }

  /** Call this on success. */
  success() {
    // Reset timer back to min after a short period.
    // If we have to `retry()` straight away, this party gets cancelled.
    this.resetTimer = setTimeout(() => {
      this.current = this.min;
    }, this.reset);
  }
}

/**
 * Wrapper around a regular WebSocket that will reopen it if it closes,
 * but with a binary backoff so it waits gradually longer each time
 * before reconnecting. This helps to avoid getting hammered by friendly
 * clients trying to reconnect if the server is closing a lot of sockets
 * for some reason (which could just be a normal redeploy).
 */
export default class RestartableWebSocket {
  constructor(url) {
    this.url = url;
    this.connected = false;
    this.onmessage = function onmessage() {};
    this.onopen = function onopen() {};

    this.backoff = new BinaryBackoff(500, 30000, 2000);

    this.backoff.retries.subscribe((ms) => {
      log.info('WS retrying connection in', ms, 'ms');
    });

    this.ensureConnection();

    this.buffer = [];
  }

  send(msg) {
    if (this.ws.readyState === 1) {
      this.ws.send(msg);
    } else {
      // Send message once we're reconnected
      this.buffer.push(msg);
    }
  }

  ensureConnection() {
    if (!this.connected) {
      const ws = new WebSocket(this.url);
      this.ws = ws;
      ws.onopen = () => {
        log.info('WS open');
        this.connected = true;
        if (this.buffer.length) {
          log.debug(`${this.buffer.length} messages were waiting to be sent. Sending...`);
        }
        this.buffer.forEach((m) => this.send(m));
        this.buffer = [];

        this.backoff.success();
        this.onopen();
      };
      ws.onclose = () => {
        log.info('WS closed');
        this.connected = false;

        if (this.explicitlyClosed) {
          log.info('Socket explicitly closed, not reopening.');
        } else {
          this.backoff.retry(() => {
            this.ensureConnection();
          });
        }
      };
      ws.onerror = (e) => {
        log.warn('WS error', e);
      };
      ws.onmessage = (msg) => {
        this.onmessage(msg);
      };
    }
  }

}
