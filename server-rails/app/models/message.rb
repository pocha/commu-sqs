class Message < ActiveRecord::Base
  def self.delete_queues
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    if ENV["RAILS_ENV"] != "production"
      queue_urls = sqs.list_queues(queue_name_prefix: "test-").queue_urls
    else
      queue_urls = sqs.list_queues.queue_urls
    end
    queue_urls.each do |queue_url|
      sqs.delete_queue(
          queue_url: queue_url
      )
    end
  end
end
