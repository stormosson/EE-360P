cmake_minimum_required(VERSION 3.0)
project (pset4)
include (../UseLATEX.cmake)

set (doc-staging-area "./doc-staging")

add_latex_document (
  4.tex
  DEFAULT_PDF
)
