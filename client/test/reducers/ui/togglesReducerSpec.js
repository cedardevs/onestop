import '../../specHelper'
import { info, initialState } from '../../../src/reducers/ui/toggles'
import { toggleAbout, toggleHelp } from '../../../src/actions/FlowActions'

describe('The ui toggles reducer',() => {
  it('toggles about',() => {
    initialState.showAbout.should.equal(false)

    const once = info(initialState, toggleAbout())
    once.showAbout.should.equal(true)

    const twice = info(once, toggleAbout())
    twice.showAbout.should.equal(false)
  })

  it('toggles help',() => {
    initialState.showHelp.should.equal(false)

    const once = info(initialState, toggleHelp())
    once.showHelp.should.equal(true)

    const twice = info(once, toggleHelp())
    twice.showHelp.should.equal(false)
  })

  it('only allows one toggles to be toggled at once', function () {
    const aboutOnce = info(initialState, toggleAbout())
    aboutOnce.showAbout.should.equal(true)
    aboutOnce.showHelp.should.equal(false)

    const helpOnce = info(aboutOnce, toggleHelp())
    helpOnce.showAbout.should.equal(false)
    helpOnce.showHelp.should.equal(true)

    const aboutTwice = info(helpOnce, toggleAbout())
    aboutTwice.showAbout.should.equal(true)
    aboutTwice.showHelp.should.equal(false)

    const aboutThrice = info(aboutTwice, toggleAbout())
    aboutThrice.showAbout.should.equal(false)
    aboutThrice.showHelp.should.equal(false)
  })
})
