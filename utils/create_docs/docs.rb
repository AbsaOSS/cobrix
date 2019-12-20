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

require 'fileutils'

module Docs
  def self.create_docs(doc_folder:, new_version:, latest_version:)
    FileUtils.cd(doc_folder, verbose: true) do
      FileUtils.cp_r(latest_version, new_version, verbose: true)

      Dir.glob("#{new_version}/*").each do |file_path|
        puts "Updating version for #{file_path}"
        file_content = File.read(file_path).partition( /---.*?(---)/m )
        new_content = file_content[0]
        new_content << file_content[1].gsub(latest_version, new_version)
        new_content << file_content[2]
        File.open(file_path, 'w') { |file| file.puts(new_content) }
      end
    end
  end

  def self.remove_docs(doc_folder:, version:)
    FileUtils.cd(doc_folder, verbose: true) do
      FileUtils.rm_r(version, verbose: true)
    end
  end

  def self.get_latest_doc_version(doc_folder:)
    FileUtils.cd(doc_folder, verbose: true) do
      Dir.glob('*').sort_by { |v| Gem::Version.new(v) }.last
    end
  end

  def self.append_version(new_version:, versions_path:)
    puts "Appending #{new_version} to versions.yaml"
    open(versions_path, 'a') { |f| f.puts "- '#{new_version}'" }
  end

  def self.reset_version_list(versions_path:, first_line: '')
    puts "Reseting/clearing versions.yaml"
    File.write(versions_path, first_line)
  end

  def self.add_topic(topic_name:, doc_folder:, yaml_path:)
    name_pretty = topic_name.tr('-', ' ').split.map(&:capitalize).join(' ')
    FileUtils.cd(doc_folder, verbose: true) do
      versions = Dir.glob('*')
      versions.each do |version|
        FileUtils.cd(version, verbose: true) do
          first_file = Dir.glob('*').first
          liquid = File.read(first_file).partition( /---.*?(---)/m )[1]
          new_liquid = liquid.gsub(/^title: \w*/, "title: #{name_pretty}")
          File.write("#{topic_name}.md", new_liquid)
        end
      end
    end

    data = YAML.load_file(yaml_path)
    data << { "name" => topic_name, "pretty_name" => name_pretty}

    File.open(yaml_path, 'w') {|f| f.write data.to_yaml }
  end

  def self.remove_topic(topic_name:, doc_folder:, yaml_path:)
    file = "#{topic_name}.md"
    FileUtils.cd(doc_folder, verbose: true) do
      versions = Dir.glob('*')
      versions.each do |version|
        file_name = "#{version}/#{topic_name}.md"
        FireUtils.rm(file_name) if File.exist?(file_name)
      end
    end

    data = YAML.load_file(yaml_path).delete_if { |d| d["name"] == topic_name }
    File.open(yaml_path, 'w') {|f| f.write data.to_yaml }
  end
end
