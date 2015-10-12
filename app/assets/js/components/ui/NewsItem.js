const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

export default class NewsItem extends ReactComponent {

    render() {
        return (
            <article className="news-item">
                <img className="news-item__image" src={this.props.imgSrc} alt={this.props.title}/>

                <h1 className="news-item__title">{this.props.title}</h1>

                <div className="news-item__body">
                    {this.props.children}
                </div>

                <div className="news-item__footer">
                    <p>
                        <a href={this.props.moreLink}>
                            Read more
                            <i className="fa fa-chevron-right"></i>
                        </a>
                    </p>

                    <p>
                        Source: {this.props.source}
                    </p>
                </div>
            </article>
        );
    }

}