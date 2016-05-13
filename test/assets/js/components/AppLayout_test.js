import { AppLayout } from 'components/AppLayout';
import TabBar from 'components/ui/TabBar';
import TabBarItem from 'components/ui/TabBarItem';

describe('AppLayout', () => {

  it('includes a tab bar item mobile', () => {
    // because we have an implicit dependency on the react router.
    const location = { pathname: '/' };
    const result = shallowRender(<AppLayout
      location={location}
      layoutClassName="mobile"
      user={({})}
      notificationsCount="7"
      onSelectItem={()=>{}} />);

    // comparing JSX with JSX. good idea? Who knows
    result.should.include(
      <TabBar selectedItem='/' onSelectItem={ () => {} }>
        <TabBarItem title="Me" icon="user" path="/" />
        <TabBarItem
          title="Notifications" icon="inbox" path="/notifications"
          badge='7' isDisabled={ true }
        />
        <TabBarItem
          title="Activity" icon="dashboard" path="/activity"
          isDisabled={ true }
        />
        <TabBarItem title="News" icon="mortar-board" path="/news" />
        <TabBarItem title="Search" icon="search" path="/search" />
      </TabBar>
    );
  })

});