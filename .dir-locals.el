;;; -*- encoding:utf-8 -*-  ---
((nil
  (cider-preferred-build-tool . boot)
  (toc-org-max-depth . 3)
  (org-link-file-path-type . relative)
  (eval add-hook 'before-save-hook #'delete-trailing-whitespace nil t)
  (cider-repl-history-file . "~/.cider-literate-clojure-repl.history")))
