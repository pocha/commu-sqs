json.array!(@messages) do |message|
  json.extract! message, :id, :app_id, :json
  json.url message_url(message, format: :json)
end
