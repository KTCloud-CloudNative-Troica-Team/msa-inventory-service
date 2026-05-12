local lastEventId = redis.call('HGET', KEYS[1], 'lastEventId')

if not lastEventId then
    return -1
end

local new_stock = redis.call('HINCRBY', KEYS[1], 'quantity', ARGV[1])

local eventId = math.max(tonumber(lastEventId), tonumber(ARGV[2]))

redis.call('HSET', KEYS[1], 'lastEventId', tostring(eventId))

return new_stock