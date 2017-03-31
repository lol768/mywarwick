import React from 'react';

// import { CuratedLinkCreateForm, CuratedLinkEditForm } from './CuratedLinkForm';

/**
 * Some fake components to override in the build, so that we don't
 * pull in a lot of Search admin script that we will never need.
 */

function NullComponent() {
  return (<div />);
}

export const CuratedLinkCreateForm = NullComponent;
export const CuratedLinkEditForm = NullComponent;
