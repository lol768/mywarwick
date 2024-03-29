// @flow

type PromiseAndCancel<T> = {
  promise: Promise<T>,
  cancel: () => void
};

export default function makeCancelable<T>(promise: Promise<T>): PromiseAndCancel<T> {
  let hasCanceled_ = false;

  const wrappedPromise = new Promise((resolve, reject) => {
    promise.then(val => (
      hasCanceled_ ? reject({ isCanceled: true }) : resolve(val)
    ));
    promise.catch(error => (
      hasCanceled_ ? reject({ isCanceled: true }) : reject(error)
    ));
  });

  return {
    promise: wrappedPromise,
    cancel() {
      hasCanceled_ = true;
    },
  };
}
