// @flow

type PromiseAndCancel<T> = {
  promise: Promise<T>,
  cancel: () => void
};

export class CancelledPromiseError extends Error {
  constructor(params) {
    super(params);

    if (Error.captureStackTrace) {
      Error.captureStackTrace(this, CancelledPromiseError);
    }

    this.name = 'CancelledPromiseError';
  }
}

export function makeCancellable<T>(promise: Promise<T>): PromiseAndCancel<T> {
  let hasCancelled_ = false;

  const wrappedPromise = new Promise((resolve, reject) => {
    promise.then(val => (
      hasCancelled_ ? reject(new CancelledPromiseError()) : resolve(val)
    ));
    promise.catch(error => (
      hasCancelled_ ? reject(new CancelledPromiseError()) : reject(error)
    ));
  });

  return {
    promise: wrappedPromise,
    cancel() {
      hasCancelled_ = true;
    },
  };
}
