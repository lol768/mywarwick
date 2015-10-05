const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const AppActions = require('../../AppActions');

const TabBarItem = require('./TabBarItem');

export default class TabBar extends ReactComponent {

    onSelectItem(item) {
        AppActions.selectTab(item.props.title);
    }

    render() {
        let tabBarItems = this.props.items.map((item) => {
            return (
                <TabBarItem key={item.title}
                            title={item.title}
                            active={this.props.selectedTab == item.title}
                            icon={item.icon}
                            badge={item.badge}
                            onClick={this.onSelectItem.bind(this)}/>
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

