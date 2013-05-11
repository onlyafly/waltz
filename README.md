[![Build Status](https://travis-ci.org/onlyafly/waltz.png?branch=master)](https://travis-ci.org/onlyafly/waltz)

# waltz

Waltz is a ClojureScript library that helps manage state in
client-side applications using non-deterministic finite state
machines.

(Note: I forked this from the original repo at
https://github.com/ibdknox/waltz since it was no longer being updated.
--Kevin Albrecht)

## Installation

Add the following dependency to your `project.clj` file:

    [onlyafly/waltz "0.1.2"]

## Usage

Here's an example using waltz, crate, jayq, and fetch:

```clojure
(ns metrics.client.main
  (:require [waltz.state :as state]
            [crate.core :as crate]
            [fetch.lazy-store :as store])
  (:use [jayq.core :only [append $ find show hide inner add-class remove-class]]
        [waltz.state :only [trigger]])
  (:use-macros [waltz.macros :only [in out defstate defevent]]
               [crate.macros :only [defpartial]]))

(defn wait [ms func]
  (js* "setTimeout(~{func}, ~{ms})"))

(def $container ($ :#metricsContainer))

(defpartial metric [{:keys [klass label]}]
  [:div {:class (str "metric " klass)}
   [:p.loading "Loading..."]
   [:h1.value "..."]
   [:h2 label]])

(defn add-metric [params]
  (let [$elem ($ (metric params))
        $loading (find $elem :.loading)
        $value (find $elem :.value)
        delay (or (:delay params) 10000)
        me (state/machine (:label params))]

    (defstate me :loading
      (in [] (show $loading))
      (out [] (hide $loading)))

    (defstate me :normal
      (in [v]
        (inner $value v)
        (wait delay #(trigger me :update))))

    (defevent me :update []
      (state/set me :loading)
      (store/latest [:metrics (:metric params)]
                    #(trigger me :set %)))

    (defevent me :set [v]
      (state/unset me :loading)
      (state/set me :normal v))

    (trigger me :update)

    (append $container $elem)
    me))

(add-metric {:label "Views today" :klass "gray"})
(add-metric {:label "Signups today" :delay 30000 :klass "gray"})
(add-metric {:label "Signups today" :delay 30000 :klass "gray"})
(add-metric {:label "Signups today" :delay 30000 :klass "gray"})
(add-metric {:label "Signups today" :delay 30000 :klass "gray big"})
(add-metric {:label "Signups today" :delay 30000 :klass "gray big"})
```

## Running Unit Tests

### Option 1: Run in browser

1. Compile the library and tests:

    ```
    $ lein cljsbuild once
    or
    $ lein compile
    ```
    
2. Open testsuite.html in your browser of choice.

### Option 2: Run using PhantomJS

1. Install PhantomJS. See http://phantomjs.org/

2. Run the tests

    ```
    $ lein cljsbuild test
    or
    $ lein test
    ```

## Todo

* New examples in README:
  * Simpler example
  * More full-featured example
* Document entire API
* Add unit tests
* Document code

## Version History

0.1.0-alpha2
* Forked from https://github.com/ibdknox/waltz
* Fixed bugs in state.cljs
* Added unit test suite

0.1.1
* Fixed debugging logs to reflect that transitions have been renamed
  to events.
  
0.1.2
* Improved debugging logs to show the context being passed into
  defstate 'in' and 'out' functions.

## License

Portions copyright (C) 2013 Kevin Albrecht

Copyright (C) 2011 Chris Granger

Distributed under the Eclipse Public License, the same as Clojure.
