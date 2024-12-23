```console
$ # Start nREPL (do it in another tab)
$ clj -M:cider-clj
$ # Build files (probably a good idea to do it in another tab)
$ clj -M:build out/
$ # Test things (you know the drill)
$ clj -M:test
$ # Continuous compilation (no need to include libraries when you can just pipe things)
$ find src/ public/js/ | entr clj -M:build out
$ # serve the files (with python http server)
$ python -m http.server -d out/
```
