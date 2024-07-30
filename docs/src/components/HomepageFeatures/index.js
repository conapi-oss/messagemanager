import clsx from 'clsx';
import Heading from '@theme/Heading';
import styles from './styles.module.css';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faUserFriends, faNetworkWired, faCode } from '@fortawesome/free-solid-svg-icons';

const FeatureList = [
  {
    title: 'Easy to Use',
    //Svg: require('@site/static/img/undraw_docusaurus_mountain.svg').default,
     icon: faUserFriends, // Replace with desired icon name
    description: (
      <>
        Message manager is easy to use and and configurable and comes with an auto-update mechanism.
      </>
    ),
  },
  {
    title: 'Multi-Broker Connectivity',
   // Svg: require('@site/static/img/undraw_docusaurus_tree.svg').default,
    icon: faNetworkWired, // Replace with desired icon name
    description: (
      <>
        No matter what broker you use, message manager will work with it. It supports major brokers and is constantly being extended.
      </>
    ),
  },
  {
    title: 'DevOps Friendly',
    //Svg: require('@site/static/img/undraw_docusaurus_react.svg').default,
     icon: faCode, // Replace with desired icon name
    description: (
      <>
        Look at messages in real time, and react to them. Message manager is a complete solution for your DevOps needs.
      </>
    ),
  },
];

/*function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>
    </div>
  );
}
*/

function Feature({ title, description, icon }) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <FontAwesomeIcon icon={icon} className={styles.featureSvg} />
      </div>
      <div className="text--center padding-horiz--md">
        <Heading as="h3">{title}</Heading>
        <p>{description}</p>
      </div>  

    </div>
  );
}
export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
