#-*- mode: ruby -*-
# Written by Eric Crosson
# 2016-01-25

require 'buildr-eclipse-launch'

VERSION_NUMBER = '0.1'
JAVA_TARGET = '1.8'

repositories.remote << "http://repo2.maven.org/maven2/"

#--- Conform to course directory structure
ass_layout = Layout.new
ass_layout[:source, :main, :java] = 'assignments/src'
ass_layout[:source, :test, :java] = 'assignments/test'

define 'assignments', :layout => ass_layout do

  #--- Project Group settings
  project.version = VERSION_NUMBER
  manifest['Copyright'] = 'Stormosson (C) 2016'
  package_with_javadoc

  #--- Build settings
  # compile.options.target = JAVA_TARGET
  doc.from projects('assignments')

  #--- Project settings
  # define 'pset1' do
  #   package :javadoc
  #   javadoc sources
  #   doc.using :windowtitle => "Abandon All Hope", :private => true
  # end

  # define 'pset2' do
  #   package :javadoc
  #   javadoc sources
  #   doc.using :windowtitle => "Abandon All Hope", :private => true
  # end

  task :default => [:build, :doc]

end
