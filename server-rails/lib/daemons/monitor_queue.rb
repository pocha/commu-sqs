#!/usr/bin/env ruby

# You might want to change this
ENV["RAILS_ENV"] = "development"

root = File.expand_path(File.dirname(__FILE__))
root = File.dirname(root) until File.exists?(File.join(root, 'config'))
Dir.chdir(root)

require File.join(root, "config", "environment")
# Load the app's custom environment variables here, so that they are loaded before environments/*.rb
app_environment_variables = File.join(root, 'config', 'app_environment_variables.rb')
load(app_environment_variables) if File.exists?(app_environment_variables)

$sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
require 'gcm'
$gcm = GCM.new("AIzaSyBJ5nCb43hO6RpUnpCkCqLydBTpal6c8uk")

#require 'logging'

#$logger = Logging.logger(STDOUT)
#$logger.level = :info

$running = true
Signal.trap("TERM") do
  $running = false
end

while($running) do
  if ENV["RAILS_ENV"] != "production"
    queue_urls = $sqs.list_queues(queue_name_prefix: "test-").queue_urls
  else
    queue_urls = $sqs.list_queues.queue_urls
  end

  queue_urls.each do |queue_url|
    Rails.logger.info "processing queue #{queue_url}"
    if message_count = $sqs.get_queue_attributes({ queue_url: queue_url, attribute_names: ["ApproximateNumberOfMessages"]})
      #send GCM to this app
      app_id = queue_url.split("/")[-1]
      app_id.slice! "test-" if ENV["RAILS_ENV"] != "production"
      $gcm.send(App.find_by(app_id: app_id).gcm_id, {dude: "get your ass off & start processing messages"})
    end
  end
  # Replace this with your code
  #Rails.logger.auto_flushing = true
  Rails.logger.info "This daemon is still running at #{Time.now}.\n"
  #$logger.error "This daemon is still running at #{Time.now}.\n"
  sleep 1
end
