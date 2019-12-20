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

class ReleaseNotes
  def initialize(version:, release:, print_empty: true)
    @liquid_separator = '---'
    @date = Time.now
    @version = version
    @print_empty = print_empty
    @release = release
  end

  def get_liquid
    liquid = {
      layout: 'default',
      title: "Release v#{@version}",
      tags: ["v#{@version}", 'changelog'],
      excerpt_separator: '<!--more-->'
    }

    liquid[:tags] << "hotfix" unless @version.split('.').last =~ /\A0.*/
    liquid
  end

  def get_release_notes
    issue_list = ""
    @release.get_issues.map do |issue|
      if issue.empty
        puts "WARN - Issue #{issue.number} has no Release Notes"
        next if !@print_empty
      end
      issue.get_release_notes
      issue_list << issue.make_release_note_string
    end

    release_notes = ""
    release_notes << @liquid_separator << "\n"
    get_liquid.each do |key, value|
      release_notes << "#{key}: #{value}\n"
    end
    release_notes << @liquid_separator << "\n\n"
    release_notes << "As of #{@date.strftime('%d/%m %Y')} the new version #{@version} is out\n"
    release_notes << "<!--more-->\n\n"
    release_notes << issue_list
  end

  def get_file_name(folder)
    "#{folder}/#{@date.strftime('%Y-%m-%d')}-release-#{@version}.md"
  end

  def write(folder)
    File.write(get_file_name(folder), get_release_notes)
  end
end
