## Wayt to setup environment  
Use pure conda to create running environment on linode ubuntu18.04 
$ conda create -n socketio   
$ conda activate socketio   
$ conda install eventlet==0.30.2  
$ conda install python-socketio  
$ conda install gunicorn  
$ pip3 install "uvicorn[standard]"    
$ uvicorn --host 0.0.0.0 --port 8144 --reload central:app
or
$ uvicorn --log-level info --host 0.0.0.0 --port 8144 --reload central:app
