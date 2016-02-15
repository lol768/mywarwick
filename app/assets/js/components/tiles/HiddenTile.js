import React from 'react';

export default class HiddenTile extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    const { icon, title } = this.props;

    return (
      <div className="tile__container col-xs-6 col-sm-6 col-md-3 tile--text-btm">
        <article className="tile tile--hidden tile--editing tile--small colour-0">
          <div
            className="tile__edit-control top-left"
            title="Show tile"
            onClick={ this.props.onAdd }
          >
            <i className="fa fa-fw fa-plus"> </i>
          </div>

          <div className="tile__wrap">
            <div className="tile__body" style={{ textAlign: 'center', marginTop: 22 }}>
              <i className={`fa fa-fw fa-${icon}`} style={{ fontSize: 36 }}> </i>
              <div className="tile__item">
                { title }
              </div>
            </div>
          </div>
        </article>
      </div>
    );
  }

}
