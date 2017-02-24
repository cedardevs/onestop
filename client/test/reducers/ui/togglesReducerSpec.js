import '../../specHelper'
import { info, initialState } from '../../../src/reducers/ui/toggles'
import { toggleAbout, toggleHelp } from '../../../src/actions/FlowActions'

describe('The ui toggles reducer',() => {
  it('toggles about',() => {
    initialState.about.should.equal(false)

    const once = info(initialState, toggleAbout())
    once.about.should.equal(true)

    const twice = info(once, toggleAbout())
    twice.about.should.equal(false)
  })

  it('toggles help',() => {
    initialState.help.should.equal(false)

    const once = info(initialState, toggleHelp())
    once.help.should.equal(true)

    const twice = info(once, toggleHelp())
    twice.help.should.equal(false)
  })

  it('only allows one toggles to be toggled at once', function () {
    const aboutOnce = info(initialState, toggleAbout())
    aboutOnce.about.should.equal(true)
    aboutOnce.help.should.equal(false)

    const helpOnce = info(aboutOnce, toggleHelp())
    helpOnce.about.should.equal(false)
    helpOnce.help.should.equal(true)

    const aboutTwice = info(helpOnce, toggleAbout())
    aboutTwice.about.should.equal(true)
    aboutTwice.help.should.equal(false)

    const aboutThrice = info(aboutTwice, toggleAbout())
    aboutThrice.about.should.equal(false)
    aboutThrice.help.should.equal(false)
  })
})
