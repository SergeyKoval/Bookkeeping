import { BkPage } from './app.po';

describe('bk App', () => {
  let page: BkPage;

  beforeEach(() => {
    page = new BkPage();
  });

  it('should display message saying app works', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('app works!');
  });
});
