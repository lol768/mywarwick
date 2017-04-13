import React, { PropTypes } from 'react';

export default class HiddenTile extends React.Component {

  render() {
    const { icon, title, onShow, colour } = this.props;

    return (
      <div className="tile__container">
        <article className={`tile tile--hidden tile--editing tile--small colour-${colour}`}>
          <div
            className="tile__edit-control top-left"
            title={ `Show ${title}` }
            onClick={ onShow }
          >
            <i className="fa fa-plus"> </i>
          </div>

          <div className="tile__wrap">
            <div className="tile__body">
              <i className={`fa fa-fw fa-${icon} tile--hidden__icon`}> </i>
              <div className="tile--hidden__title">{ title }</div>
            </div>
          </div>
        </article>
      </div>
    );
  }

}

HiddenTile.propTypes = {
  icon: PropTypes.string,
  title: PropTypes.string,
  onShow: PropTypes.func,
  colour: PropTypes.number,
};
