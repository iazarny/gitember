Why I Replaced JavaFX with Swing for a Git GUI

Back in 2016, I started Gitember as a small experiment. The goal was simple: build a clean, fast Git desktop client in Java.

At the time, JavaFX looked like the obvious choice. A modern UI toolkit, CSS styling, rich components — everything you’d expect for building desktop apps.

However, when you look closer, some essential components are missing. For example, there’s no proper rich text editor out of the box, so you end up relying on third-party libraries. This adds extra complexity and reduces the benefits of using a “batteries-included” UI framework.

The Problem Was Features — and Weight
Everything worked fine while it was a small, experimental project. But as it grew beyond a “toy” stage, the limitations started to show.

Everything worked fine while it was a small, experimental project. But as it grew beyond that stage, the limitations started to show.

JavaFX also being removed from the JDK starting with Java 11 made things more complicated. It introduced additional dependencies, extra setup, and more complex packaging — all of which added friction to what was supposed to be a simple desktop tool.

Why Google did normal UI for Android , but Oracle wsa not able ? 

As Gitember grew into a full-featured Git GUI, one issue kept coming back:

It felt heavier than it should.

Not in terms of functionality, but in:

startup time

memory usage

distribution size

runtime complexity

For a Git client, this matters more than you think.

A Git tool is something you open frequently, often for quick operations:

check diff

commit changes

switch branches

If it takes a few seconds to start, it breaks the flow.

JavaFX Trade-offs

JavaFX is powerful, but it comes with trade-offs:

additional runtime dependencies

packaging complexity

CSS engine overhead

less predictable performance across environments

None of these are deal-breakers for many apps. But for a fast, lightweight utility, they add up.

Why Swing?

Swing is often considered “old”, but it has some properties that are hard to ignore:

part of the standard JDK

no extra runtime

very predictable behavior

fast startup

low memory overhead

For Gitember, these characteristics turned out to be more important than modern UI abstractions.

The Rewrite

For version 3, I made a decision:
rewrite the entire UI layer from JavaFX to Swing.

Not a small refactor — a full rebuild.

The goal was simple:

Make the app feel instant.

The Result

After the rewrite:

startup time improved significantly

installation size decreased

memory usage dropped

UI responsiveness improved

More importantly, the app now behaves like a native utility:
open → do the job → close

No waiting, no friction.

But What About UX?

Yes, Swing is less “modern” than JavaFX.

But for a Git client, priorities are different:

speed over animations

clarity over styling flexibility

responsiveness over visual effects

With careful design, Swing is more than capable of delivering a clean, usable UI.

Features Stayed — Just Faster

The rewrite didn’t remove functionality. Gitember still includes:

integration with GitHub, GitLab, Bitbucket, Gitea

diff viewer (unified, side-by-side, context)

folder and file comparison

full-text search (commits, code, documents — including Office and CAD files)

repository statistics

Git LFS support

The difference is that everything now runs lighter.

Lessons Learned

Technology choice depends on the product type
What works for enterprise apps may not work for small tools.

Startup time is a feature
Especially for utilities used dozens of times per day.

“Modern” is not always better
Sometimes older tech fits the problem better.

Simplicity wins in desktop tools
Fewer layers → fewer problems.

Final Thoughts

This wasn’t about Swing vs JavaFX as a general debate.

It was about choosing the right tool for the job.

For Gitember, Swing made it possible to deliver what I originally wanted back in 2016:
a fast, simple, no-friction Git client.

If you’re building desktop tools, I’m curious:

How much does startup time matter in your workflow?

👉 https://gitember.org