import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class NewsItem extends ReactComponent {

    render() {
        let image = this.props.imgSrc ?
            <img className="news-item__image" src={this.props.imgSrc} alt={this.props.title}/> : null;

        return (
            <article className="news-item">
                {image}

                <div className="news-item__body">
                    <h1 className="news-item__title">
                        <a href={this.props.url} target="_blank">
                            {this.props.title}
                        </a>
                    </h1>

                    <div className="news-item__content">
                        {this.props.children}
                    </div>

                    <div className="news-item__footer">
                        <p>
                            <i className="fa fa-fw fa-circle" style={{color: '#7ecbb6'}}></i>
                            {this.props.source}
                        </p>
                    </div>
                </div>
            </article>
        );
    }

}