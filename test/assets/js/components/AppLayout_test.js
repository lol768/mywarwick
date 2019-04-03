import { AppLayout } from 'components/AppLayout';
import TabBar from 'components/ui/TabBar';
import TabBarItem from 'components/ui/TabBarItem';

describe('AppLayout', () => {

  const location = { pathname: '/' };
  const props = {
    location,
    user: ({}),
    features: { news: true },
    notificationsCount: '7',
    onSelectItem: () => {
    },
  };

  it('includes a tab bar item mobile', () => {
    // because we have an implicit dependency on the react router.
    const result = shallowRender(
      <AppLayout
        {...props}
      />);

    // comparing JSX with JSX. good idea? Who knows
    result.should.include(
      <TabBar selectedItem='/' onSelectItem={() => {
      }}>
        <TabBarItem title="Me" icon="user" selectedIcon="user" path="/"/>
        <TabBarItem
          title="Alerts" icon="bell" selectedIcon="bell" path="/alerts"
          badge='7' isDisabled={true}
        />
        <TabBarItem
          title="Activity" icon="tachometer" selectedIcon="tachometer" path="/activity"
          isDisabled={true}
        />
        <TabBarItem
          title="News" icon="newspaper" selectedIcon="newspaper" path="/news"/>
        <TabBarItem title="Search" icon="search" selectedIcon="search" path="/search"/>
      </TabBar>
    );
  });

  it('does not include news tabbar itme if it is disabled', () => {
    // because we have an implicit dependency on the react router.
    const result = shallowRender(
      <AppLayout
        {
          ...{
            ...props,
            features: { news: false }
          }
        }
      />);

    // comparing JSX with JSX is a good idea
    result.should.include(
      <TabBar onSelectItem={function noRefCheck() {}} selectedItem="/" >
        <TabBarItem icon="user" path="/" selectedIcon="user" title="Me" />
        <TabBarItem badge="7" icon="bell" isDisabled={true} path="/alerts" selectedIcon="bell" title="Alerts" />
        <TabBarItem icon="tachometer" isDisabled={true} path="/activity" selectedIcon="tachometer" title="Activity" />
        <TabBarItem icon="search" path="/search" selectedIcon="search" title="Search" />
      </TabBar>
    );
  })

});
