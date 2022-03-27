import socketio
import random
import string

sio = socketio.AsyncServer(async_mode='asgi', cors_allowed_origins='*')
app = socketio.ASGIApp(sio, static_files = {
    '/': './slideio.html',
    '/slide-wallet':'./slide-wallet.html',
    '/google-login':'./google-login.html'
})


client_count = 0
room_counts = {}
data_logic = {
    "allrooms":['test'],
    "room_objects":{
        'test':{
            'url':'https://www.google.com',
            'count':1,
            'sids':['sometestSID']
        } # test room show template of each room_object corresponding to room name
    }
}

async def task(sid):
    print('task(sid='+str(sid)+')' )
    await sio.sleep(5)
    print('after sio.sleep(5) and ready to emit' )
    result = await sio.call('mult', {'numbers': [3, 4]}, to=sid)
    print(result)

@sio.event
async def connect(sid, environ):
    global client_count
    global room_counts

    username = environ.get('HTTP_X_USERNAME')
    print('username:', username)
    if not username:
        return False
    
    async with sio.session(sid) as session:
        session['username'] = username
    await sio.emit('user_joined', username)

    client_count += 1
    print(sid, 'connected')
    sio.start_background_task(task, sid)
    await sio.emit('client_count',  client_count)
    roomNum = random.randint(0,1)
    roomName = "room" + str(roomNum)
    sio.enter_room(sid, roomName)
    room_counts[roomName] = room_counts.get(roomName, 0) + 1
    await sio.emit('room_name', roomName, to=sid)
    await sio.emit('room_count', room_counts[roomName], to=roomName)


@sio.event
async def disconnect(sid):
    global client_count
    client_count -= 1
    print(sid, 'disconnected')
    await sio.emit('client_count', client_count)
    leaveallrooms_when_disconnect(sid)
    for roomName in room_counts.keys():
        if roomName in sio.rooms(sid):
            room_counts[roomName] -= 1
            await sio.emit('room_count', room_counts[roomName], to=roomName)
    async with sio.session(sid) as session:
        await sio.emit('user_left', session['username'])

@sio.event
async def sum(sid, data):
    print(sid, data)
    result = data['numbers'][0] + data['numbers'][1]
    # await sio.emit('sum_result', {'result': result}, to=sid)
    return {'result': result}



@sio.event
async def joinroom(sid, room):
    """
    data_logic = {
        "allrooms":['test'],
        "room_objects":{
            'test':{
                'url':'https://www.google.com',
                'count':1,
                'sids':['sometestSID']
            } # test room show template of each room_object corresponding to room name
        }
    }
    """
    global data_logic
    room_objects = data_logic["room_objects"]
    allrooms = data_logic["allrooms"]
    print('joinroom called')
    print(sid, room)
    if not (room in allrooms):
        result = {'sid':sid, 'room':'failed'}
        return result # use room name as failed to say failed  
    room_object = room_objects[room]
    room_object["sids"].append(sid)
    room_object["count"] += 1
    sio.enter_room(sid, room)
    result = {'sid': sid, 'room': room, 'url': room_object["url"]}
    # Expect client receive this result and sync this slide url
    return result

# Will deprecate this code in future and let client to use joinroom
# Our current unit test need it

def avaliable_random_room(room:str)->str:
    global data_logic
    allrooms = data_logic["allrooms"]
    newroom = room
    #newroom = 'test' #set test tring here and put 'test in default allrooms, then you can test above code
    while newroom in allrooms:
        random_four_digits:str = ''.join(random.choice(string.digits) for x in range(4)) # four random digits
        newroom = random_four_digits
    return newroom

@sio.event
async def createroom(sid, data):
    global data_logic
    room = data['room']
    url = data['url']
    room = avaliable_random_room(room) #create new room name if room is exist in data_logic['allrooms']
    print('createroom called')
    print(sid, room)
    allrooms = data_logic["allrooms"]
    room_objects = data_logic["room_objects"]
    allrooms.append(room)
    new_room_object = {"url": url, "count": 1, "sids": [sid]}
    room_objects[room] = new_room_object
    print(room_objects)
    sio.enter_room(sid, room)
    result = {'sid': sid, 'room': room, 'url': url}
    return result


def request_recycle_room_check_process():
    # recycle the room with count as zero for next time to create
    # Assume allow every request.
    # imrpove here for check current condiction to decide allow this request or not
    global data_logic
    room_objects = data_logic["room_objects"]
    allrooms = data_logic["allrooms"]
    remove_rooms = []
    items = room_objects.items()
    for room, room_object in items:
        if room_object['count'] <= 0:
            #allrooms.remove(room)
            #del room_objects[room]
            remove_rooms.append(room)
    for room in remove_rooms:
        allrooms.remove(room)
        del room_objects[room]

def leaveallrooms_when_disconnect(sid):
    # clean all related data of rooms that related to this sid in data_logic
    global data_logic
    room_objects = data_logic["room_objects"]
    for room, room_object  in room_objects.items():
        if sid in room_object["sids"]:
            room_object['count'] -= 1
            room_object['sids'].remove(sid)

    # For performance consideration, It's may not clean everytime to disconnected here
    request_recycle_room_check_process()
    pass

# This function might not used for client to call 
@sio.event
async def leaverooms(sid, rooms):
    global data_logic
    print('leaverooms called')
    print(sid, rooms)
    for room in rooms:
        sio.leave_room(sid, room)
        room_objects = data_logic["room_objects"]
        room_object = room_objects[room]
        room_object['count'] -= 1
    result = {'sid': sid, 'rooms': rooms}
    return {'result': result}

@sio.event
async def leaveroom(sid, room):
    global data_logic
    print('leaveroom called')
    print(sid, room)
    sio.leave_room(sid, room)
    room_objects = data_logic["room_objects"]
    room_object = room_objects[room]
    room_object['count'] -= 1
    result = {'sid': sid, 'room': room}
    return result

@sio.event
async def multicast(sid, data):
    """
    data_logic = {
        "allrooms":['test'],
        "room_objects":{
            'test':{
                'url':'https://www.google.com',
                'count':1,
                'sids':['sometestSID']
            } # test room show template of each room_object corresponding to room name
        }
    }
    """
    global data_logic
    print('multicast called')
    print(sid, data)
    room = data['room']
    msg = data['msg']
    channel = data['channel']

    room_objects = data_logic["room_objects"]
    room_object = room_objects[room]
    room_object["url"] = msg #It's url. but not url for testing mode ... dirty


    await sio.emit(channel, data, to=room)
    result = {'sid': sid, 'data': data}
    return result

