#!/bin/bash
lein test :default && lein install && cd demo && lein run
