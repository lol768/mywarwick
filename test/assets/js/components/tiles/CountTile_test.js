import CountTile from 'components/tiles/CountTile';
import { TILE_SIZES } from '../../../../../app/assets/js/components/tiles/TileContent';

describe('CountTile', () => {

  function checkCountBehaviour(props, expectedCount){
    const html = shallowRender(<CountTile { ...props } />);
    html.type.should.equal('div');
    html.props.className.should.equal('tile__item');
    const [ count, word ] = html.props.children;
    count.type.should.equal('span');
    count.props.className.should.equal('tile__callout');
    count.props.children.should.equal(expectedCount);
    word.type.should.equal('span');
    word.props.className.should.equal('tile__text');
    word.props.children.should.equal(props.content.word);
  }

  it('should show the count and word', () => {
    const props = {
      content: {
        count: 53,
        word: 'herons',
      },
      size: TILE_SIZES.SMALL,
    };
    checkCountBehaviour(props, props.content.count);
  });

  it('should show the length of items and word', () => {
    const props = {
      content: {
        items: ['a', 'b', 'c'],
        word: 'letters',
      },
      size: TILE_SIZES.SMALL,
    };
    checkCountBehaviour(props, 3);
  });

  it('should show a list of items when zoomed', () => {
    const props = {
      zoomed: true,
      content: {
        items: [{id: 1}, {id: 2}],
        word: 'objects',
      },
      size: TILE_SIZES.SMALL,
    };
    const html = shallowRender(<CountTile { ...props } />);
    html.type.should.equal('ul');
    html.props.children.length.should.equal(2);
  });
});
