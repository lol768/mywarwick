import ShowMore from 'components/tiles/agenda/ShowMore';

describe('ShowMore', () => {
  const items = [1,2,3,4,5];
  const noop = ()=>{};

  const LINK_SELECTOR = '.text-right a';

  it('Renders nothing if there are no more', () => {
    const result = shallowAtMoment(<ShowMore items={items} showing={5} onClick={noop} />);
    result.isEmptyRender().should.equal(true);
  });

  it('Renders link if there are some more', () => {
    const result = shallowAtMoment(<ShowMore items={items} showing={2} onClick={noop} />);
    const link = result.find(LINK_SELECTOR);
    link.text().should.equal('+3 more');
    link.prop('onClick').should.equal(noop);
  });
});