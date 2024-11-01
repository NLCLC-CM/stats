```console
$ # Start nREPL (do it in another tab)
$ clj -M:cider-clj
$ # Build files (probably a good idea to do it in another tab)
$ clj -M:build _out/
$ # Test things (you know the drill)
$ clj -M:test
$ # Continuous compilation (no need to include libraries when you can just pipe things)
$ find src/ | entr clj -M:build out
```
