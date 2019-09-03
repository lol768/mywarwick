export function hasAuthoritativeUser(state) {
  const u = state.user;

  return u && u.authoritative === true;
}

export function hasAuthoritativeAuthenticatedUser(state) {
  return hasAuthoritativeUser(state) && state.user.data.authenticated === true;
}
