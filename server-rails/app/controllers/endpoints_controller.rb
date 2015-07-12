class EndpointsController < ApplicationController
  def deliver
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    queue_name = params[:app_id]
    if ENV["RAILS_ENV"] != "production"
      queue_name = "test-#{queue_name}"
    end
    queue = sqs.queues.named(queue_name)
    queue.send_message(params[:json])
  end

  def register
    App.create(params)
    #create new queue with app_id as name
    sqs = Aws::SQS::Client.new(region: ENV["AWS_region"])
    queue_name = params[:app_id]
    if ENV["RAILS_ENV"] != "production"
      queue_name = "test-#{queue_name}"
    end
    queue = sqs.queues.create(queue_name, visibility_timeout: 90, maximum_message_size: 262144)
  end

 end
