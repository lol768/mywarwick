const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const TabBarItem = require('./TabBarItem');

export default class TabBar extends ReactComponent {

    render() {
        let tabBarItems = this.props.items.map((item) => {
            return (
                <TabBarItem key={item.path}
                            active={this.props.selectedItem == item.path}
                            title={item.title}
                            icon={item.icon}
                            badge={item.badge}
                            path={item.path}
                            onClick={() => this.props.onSelectItem(item.path)}/>
            );
        });

        return (
            <nav className="tab-bar">
                <ul className="tab-bar-tabs">
                    {tabBarItems}
                </ul>
            </nav>
        );
    }

}

