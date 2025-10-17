// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Message Manager.',
  tagline: 'Your universal messaging client.',
  favicon: '/img/logo.png',

    // ... https://conapi-oss.github.io/messagemanager/
  url: 'https://conapi-oss.github.io/', // Your website URL
  baseUrl: '/messagemanager/',
  projectName: 'messagemanager',
  organizationName: 'conapi-oss',
  trailingSlash: false,
  // ...

  // Set the production url of your site here
  //url: 'https://www.conapi.at',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  //baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  //organizationName: 'conapi GmbH', // Usually your GitHub org/user name.
  //projectName: 'messagemanager', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: './sidebars.js',
          // alternate approach sample: https://github.com/continuedev/continue/blob/main/docs/sidebars.js


          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          //   editUrl:
          //   'https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      // Replace with your project's social card
      image: 'img/mm-social-card.png',
      navbar: {
        title: 'Message Manager',
        logo: {
          alt: 'Message Manager Logo',
          src: 'img/logo.png',
        },
        items: [
          {
            type: 'docSidebar',
            sidebarId: 'mmSidebar',
            position: 'left',
            label: 'Documentation',
          },
          //{to: '/blog', label: 'Blog', position: 'left'},
          {
            href: 'https://github.com/conapi-oss/messagemanager',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Tutorial',
                to: '/docs/intro',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'conapi Website',
                href: 'https://www.conapi.at',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/conapi-oss/messagemanager',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} conapi GmbH. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
    }),
};

export default config;
