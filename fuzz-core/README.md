# fuzz-core

Standard generators for every `schema-std` type, standard mutators, a byte shrinker, and the round trip and mutation
runners.

`Fuzz.roundTrip` and `Fuzz.mutate` run them with the standard setup.

Depends on `fuzz-api`, `schema-std` and `buffer-nio`.
