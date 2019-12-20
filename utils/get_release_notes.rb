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

require 'uri'
require 'net/http'
require 'openssl'
require 'json'
require_relative 'get_release_notes/issue'
require_relative 'get_release_notes/opt_parser'
require_relative 'get_release_notes/release_notes'
require_relative 'get_release_notes/release'

OptParser.parse(ARGV)

PAGES_ROOT = File.expand_path('..', __dir__)
POST_FOLDER = "#{PAGES_ROOT}/_posts"

class String
  def remove_first_line
    first_newline = (index("\n") || size - 1) + 1
    slice!(0, first_newline).sub("\n",'')
    self
  end
end

def call(url)
  http = Net::HTTP.new(url.host, url.port)
  http.use_ssl = true
  http.verify_mode = OpenSSL::SSL::VERIFY_NONE

  request = Net::HTTP::Get.new(url)
  request["x-authentication-token"] = OptParser.options.zenhub_token
  request["Authorization"] = OptParser.options.github_token
  request["content-type"] = 'application/json'
  response = http.request(request)
  body_json = JSON.parse(response.body)
  keys_to_symbols(body_json)
end

def keys_to_symbols(whatever)
  if whatever.is_a?(Hash)
    whatever.inject({}) do |acc,(k,v)|
      acc[k.to_sym] = v
      acc
    end
  elsif whatever.is_a?(Array)
    whatever.inject([]) do |acc, v|
      acc << (v.is_a?(Hash) ? keys_to_symbols(v) : v)
      acc
    end
  else
    whatever
  end
end

def get_release_object(uri:, klass:)
  all_releases = call(uri)
  needed_release = all_releases.select { |v| v[:title] == OptParser.options.version }.last
  raise ArgumentError, 'Version Not Found for release notes', caller if needed_release.nil?
  klass.create(needed_release)
end

choosen_release = if OptParser.options.use_zenhub
  get_release_object(uri: URI("#{OptParser.options.zenhub_url}/p1/repositories/" +
                          "#{OptParser.options.repository_id}/reports/releases") ,
                     klass: ZenhubRelease)
else
  get_release_object(uri: URI("#{OptParser.options.github_url}/milestones?state=all"),
                     klass: GithubRelease)
end

release_notes = ReleaseNotes.new(version: OptParser.options.version,
                                 release: choosen_release,
                                 print_empty: OptParser.options.print_empty)

release_notes.write(POST_FOLDER)

