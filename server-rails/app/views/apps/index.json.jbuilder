json.array!(@apps) do |app|
  json.extract! app, :id, :app_id, :gcm
  json.url app_url(app, format: :json)
end
