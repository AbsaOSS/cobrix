# Copyright 2018-2019 ABSA Group Limited
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require 'optparse'

class OptParser

  ROOT_PATH = "#{File.expand_path('../..', __dir__)}"
  GIT_PATH = "#{ROOT_PATH}/.git"

  require 'git' if File.exist?(GIT_PATH)

  def self.options
    @@options
  end

  def self.parse(args)
    app_conf_file_path = "#{File.expand_path('..', __dir__)}/resources/get_release_notes.json"
    app_conf = if (File.exist?(app_conf_file_path))
      file = File.read(app_conf_file_path)
      JSON.parse(file, symbolize_names: true)
    else
      {}
    end

    options = OpenStruct.new
    options.use_zenhub = app_conf[:use_zenhub]
    options.print_empty = app_conf[:print_empty]
    options.print_only_title = app_conf[:print_only_title]
    options.organization = app_conf[:organization]
    options.repository = app_conf[:repository]
    options.repository_id = app_conf[:repository_id]
    options.zenhub_url = app_conf[:zenhub_url]
    options.github_url = app_conf[:github_url]
    options.strict = app_conf[:strict]
    options.github_token = ENV['GITHUB_TOKEN'] || app_conf[:github_token]
    options.zenhub_token = ENV['ZENHUB_TOKEN'] || app_conf[:zenhub_token]
    options.version = args.shift

    if File.exist?(GIT_PATH)
      remote_url = Git.open(ROOT_PATH).remote.url
      options.organization = remote_url[/(?<=:)\w*/] if options.organization.nil?
      options.repository = remote_url[/(?<=\/)\w*/] if options.repository.nil?
    end

    opt_parser = OptionParser.new do |opts|
      opts.banner = "Usage: ruby utils/get_release_notes.rb VERSION [options]"

      opts.separator ""
      opts.separator "Specific options:"

      opts.on("--github-token TOKEN", 'Github token. Can be specified using environment variable GITHUB_TOKEN ' +
                                      'or in get_release_notes.json in resources using github_token key') do |gt|
        options.github_token = "token #{gt}"
      end

      opts.on("--zenhub-token TOKEN", 'Zenhub token. This means we will use ' +
                                      'Release object for release notes. You don\'t '+
                                      'have to use --use-zenhub in case you do this. ' +
                                      'Can be specified using environment variable ZENHUB_TOKEN ' +
                                      'or in get_release_notes.json in resources using zenhub_token key') do |zt|
        options.use_zenhub = true
        options.zenhub_token = zt
      end

      opts.on("-z", "--use-zenhub", 'Run using zenhub. It needs zenhub token set.' +
                                    ' If you use --zenhub-token option, you don\'t need to use this.' +
                                    ' This means we will use Release object for release notes.') do |z|
        if options.zenhub_token.nil?
          raise ArgumentError, "Can't find ZENHUB_TOKEN environemnt variable", caller
        end
        options.use_zenhub = z
      end

      opts.on('--organization ORGANIZATION', 'Github Organization') do |org|
        options.organization = org
      end

      opts.on('--repository REPOSITORY', 'Github Repository name') do |repo|
        options.repository = repo
      end

      opts.on('--repository-id REPOSITORYID', 'Zenhub Repository ID') do |repo_id|
        options.repository_id = repo_id
      end

      opts.on('--zenhub-url ZENURL', 'Zenhub API URL') do |url|
        options.zenhub_url = url
      end

      opts.on('--github-url GITURL', 'Github API URL') do |url|
        options.github_url = url
      end

      opts.on('-p', '--[no-]print-empty', 'Should Issue with no release notes comment be ' +
                                          'included in the output file') do |p|
        options.print_empty = p
      end

      opts.on('--[no-]print-only-title', 'Should Issue with no release notes comment be ' +
                                         'preceeded with \'Couldn\'t find comment\'') do |p|
        options.print_only_title = p
      end

      opts.on('-s', '--[no-]strict', 'Treats warnings as errors') do |s|
        options.strict = s
      end

      opts.on_tail("-h", "--help", "Show this message") do
        puts opts
        exit
      end
    end

    if options.version =~ /\A(-h|--help)/
      opt_parser.parse!(['-h'])
    elsif options.version.nil?
      puts 'WARN You did not specifie mandatory version parameter. Here is --help'
      opt_parser.parse!(['-h'])
    end

    # Version string can start with a small v, then version numbers X.Y.Z, after that you can follow
    # with -RCX where dash is mandatory, RC can be downcase or uppercase and X represents any number
    # higher then zero
    unless options.version =~ /\Av?([0-9]+\.){2}[0-9]+(-(R|r)(C|c)[1-9][0-9]*)?\Z/
      raise OptionParser::InvalidArgument, "Invalid Version argument - #{options.version}", caller
    end

    opt_parser.parse!(args)

    if options.organization.nil?
      raise OptionParser::InvalidArgument, "Organization needs to be set either in the config file" +
                                            " or as and argument", caller
    end

    if options.repository.nil?
      raise OptionParser::InvalidArgument, "Repository needs to be set either in the config file" +
                                            " or as and argument", caller
    end

    if options.github_url.nil?
      options.github_url = "https://api.github.com/repos/#{options.organization}/#{options.repository}"
    elsif options.github_url =~ /\Ahttps:\/\/api.github.com\Z/
      options.github_url = "#{options.github_url}/repos/#{options.organization}/#{options.repository}"
    end

    @@options = options
    options
  end
end
