# Testing The GitHub Pages Site Locally
## Getting Setup
The first course of action you should take to build and test the pages locally is to follow the [official Github documentation](https://help.github.com/en/github/working-with-github-pages/testing-your-github-pages-site-locally-with-jekyll). 

Midway through the documentation, it is recommended to use RVM or Homebrew to manage your Ruby installation. If you are using a Mac, the Homebrew route is the way to go -- and will likely require you to modify your PATH (Macs have Ruby pre-installed but it's probably an older version than you want).

If you encounter this issue:
```bash
$ bundle exec jekyll serve
Could not locate Gemfile or .bundle/ directory
```
You'll need to create a Gemfile in the build directory. In the case of this repo, that's the `docs` folder. The contents of the Gemfile:
```bash
$ cat Gemfile
gem 'github-pages', group: :jekyll_plugins
```

Now you should be able to build and run the site locally, avoiding the need for painful troubleshooting on GitHub (error messages are rare, you've been warned!).
```bash
$ bundle exec jekyll serve
Configuration file: /Users/neo/git/onestop/docs/_config.yml
            Source: /Users/neo/git/onestop/docs
       Destination: /Users/neo/git/onestop/docs/_site
 Incremental build: disabled. Enable with --incremental
      Generating... 
                    done in 0.864 seconds.
 Auto-regeneration: enabled for '/Users/neo/git/onestop/docs'
    Server address: http://127.0.0.1:4000
  Server running... press ctrl-c to stop.
```

Finally, when you're editing a page locally, you will need to manually reload pages to see your edits as Jekyll does not auto-reload for you. In some cases, as you're working on new pages, Jekyll may not render an `.md` link as an HTML page -- just restart the Jekyll server to rebuild pages in this case.

## Page Standards

### Estimated Reading Time on pages
Add estimated reading time to top of articles -- https://niram.org/read/ -- and round to the nearest minute. Keep in mind an article with a lot of images might have an inflated estimate due to alternative text and URLs, so use your best judgement here.
`**Estimated Reading Time: 10 minutes**`

### Generate a TOC 
Once you've finished an article, it might be helpful to add a table of contents at the top for quick access. This site makes doing so quick and easy: https://imthenachoman.github.io/nGitHubTOC/

## Errors Encountered
If you run into something frustrating somewhere and figure out how to fix it, save your teammates some grief and add your fix below!
### Liquid Error When No One's Using Liquid To Begin With...
Jekyll uses Liquid for some site templating functionality, which is great if you're using it, but not great if you add a file with something Liquid thinks is Liquid even though it's not. Think code snippet with another templating language being used within. In order to avoid this, you'll have to [wrap offending text inside some tags](https://github.com/jekyll/jekyll/blob/master/docs/_docs/liquid/tags.md#code-snippet-highlighting).

The tags are visible when looking at rendered Markdown but disappear when GitHub renders the page. Example:

{% raw %}
```
# helm/psi-registry/templates/statefulset.yaml
...
    env:
    # EXPORT ACTIVE SPRING PROFILES TO TELL SPRING WHICH FEATURES TO ENABLE
    # the loop is making a comma delimited list for multi-feature handling
    - name: SPRING_PROFILES_ACTIVE
      value: '{{ $active := dict "profiles" (list) -}}
              {{- range $feature, $enabled := .Values.features -}}
                {{- if $enabled -}}
                  {{- $noop := $feature | append $active.profiles | set $active "profiles" -}}
                {{- end -}}
              {{- end -}}
              {{- join "," $active.profiles }}'
...
```
{% endraw %}

Another tip that sounded good but didn't work (though YMMV): 
>you can add `render_with_liquid: false` in your front matter to disable Liquid entirely for a particular document

