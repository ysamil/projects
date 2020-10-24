from raritan import rpc 
from raritan.rpc import pdumodel
from raritan.rpc import sensors
import random, time, datetime, math, sys, os, traceback
import pexpect, pickle, logging, argparse

DEVICE_CURRENT_MULTIPLIER = 0.8
PERCENTAGE_OF_RANGE_CURRENT = 5 #Random values will be generated in this percentage of initial values. (e.g. = +-%3)
PERCENTAGE_OF_RANGE_VOLTAGE = 2

logging.basicConfig(filename='log-file',level=logging.DEBUG, format='%(asctime)s,%(msecs)d %(levelname)-8s [%(filename)s:%(lineno)d] %(message)s', 
    datefmt='%Y-%m-%d:%H:%M:%S')

parser = argparse.ArgumentParser(usage="randomization.py [-h] [-u url] [-s SLOW] [-d DEBUG] [-l LAST]")
parser.add_argument("url", type=str, help="Url to connect to the server. It should be in this form: [scheme://][user[:password]@]host")
parser.add_argument("-s", "--slow",  required = False,type=float, action = "store", help = "To slow randomization time in seconds should be given. Ex: '-s 0.5'")
parser.add_argument("-d", "--debug",  required = False, action = "store_true", help = "The errors are printed to the console.")
parser.add_argument("-l", "--last",  required = False, action = "store_true", help = "Randomization starts from the last saved values.")
parser.add_argument("-t", "--thresh",  required = False, action = "store_true", help = "For upper critical current, threshold values will be taken into account.")
args = parser.parse_args()

def parse_url(url):
    scheme = 'https'
    username = 'admin'
    password = 'raritan'
    hostname = url

    parts = hostname.split('://')
    if len(parts) >= 2:
        (scheme, hostname) = parts  
    parts = hostname.split('@')
    if len(parts) >= 2:
        (username, hostname) = parts    
    parts = username.split(':')
    if len(parts) >= 2:
        (username, password) = parts    
    return (scheme, hostname, username, password)

try:
	(scheme, hostname, username, password) = parse_url(args.url)
except Exception:
	print("Url should be in this form: [scheme://][user[:password]@]host")
	sys.exit(1)

class ConnectLua():
    def __init__(self, host):
        self.host = host
        self.open()
    
    def open(self):
        self.spawn = pexpect.spawn("nc %s 20623" % self.host)
        self.spawn.timeout = 2
        self.spawn.expect_exact("> ")

    def run(self, cmd):
        self.spawn.sendline(cmd)
        self.spawn.expect_exact(cmd)
        self.spawn.expect_exact("\r\n> ")
        return self.spawn.before.strip().decode("utf-8")

#The function which generates random values in a certain range.
def randomInRange(value, percentageOfRange):
    percentageOfValue = (value * percentageOfRange) / 100
    randomUpperLimit = value + percentageOfValue
    randomLowerLimit = value - percentageOfValue
    randomValue = random.uniform(-1 * percentageOfValue, percentageOfValue)
    value += randomValue
    if value > randomUpperLimit :
        value = randomUpperLimit
    elif value < randomLowerLimit :
        value = randomLowerLimit
    return value

random.seed() 
#This function get random values from the function above and calculate the others with this random values.
def calculateValues(index):
    global randCurrent, randVoltage, phaseAngle, activePower, apparentPower, activeEnergy, powerFactor, reactivePower
    randCurrent = randomInRange(initialCurrents[index], PERCENTAGE_OF_RANGE_CURRENT)
    randVoltage = randomInRange(initialVoltages[index], PERCENTAGE_OF_RANGE_VOLTAGE)   
    phaseAngle = random.randint(0,30)
    activePower = randVoltage * randCurrent * math.cos(math.radians(phaseAngle))
    apparentPower = randVoltage * randCurrent 
    reactivePower = math.sqrt((apparentPower * apparentPower) - (activePower * activePower))
    powerFactor = activePower / apparentPower
    if timeStamps[index] != None : 
        now = datetime.datetime.now()
        t = (now - timeStamps[index]).total_seconds()
        activeEnergy = int(activePower * t)

try:
    agent = rpc.Agent(scheme, hostname, username, password, disable_certificate_verification = True)
    pdu = pdumodel.Pdu("/model/pdu/0", agent)
    pduMetaData = pdu.getMetaData()
    try:
        inlets = pdu.getInlets()
        outlets = pdu.getOutlets()
        connectLua = ConnectLua(hostname)
        hasMeteredOutlets = pduMetaData.hasMeteredOutlets
        isInlineMeter = pduMetaData.isInlineMeter
        poleControlled = False
        tmp = connectLua.run("print(help())")
        if "pole numbers" in tmp:
            poleControlled = True


        #Calculating the upper critical current 
        deviceCurrent = (float((pduMetaData.nameplate.rating.current).split("A")[0])) * DEVICE_CURRENT_MULTIPLIER # multiply by some value lower than 1, to approximate to the upper critical value
        hasNeutral = 0
        if poleControlled:
            upperCriticalCurrent = deviceCurrent
        else :
            numberOfPoles = 0 
            for inlet in inlets:
                poles = inlet.getPoles()
                if len(poles) > 2 :
                    for pole in poles:
                        if str(pole.line) != "pdumodel.PowerLine.NEUTRAL":
                            numberOfPoles +=1
                        else:
                            hasNeutral = 1
                else:
                    numberOfPoles +=1
            upperCriticalCurrent = deviceCurrent * numberOfPoles
        #Properties are set regarding to device type
        if hasMeteredOutlets:
            meter = outlets
            meterType = "outlets"
            upperCriticalCurrent = upperCriticalCurrent / len(outlets)
        else:
            meter = inlets
            meterType = "inlets"

        while True:
            #If user gives "-l" as argument, then initial currents and voltages are received from record file. 
            #Random values will be generated in a certain range (percentageOfRange) depends on these last values.
            startFromLast = args.last
            if startFromLast:
                try :
                    if os.path.getsize("last-values") == 0:
                        print("\nRecord file is empty. Please start with new values.")
                        sys.exit(1)
                    with open("last-values", "rb") as filehandler:
                        oldPduModel = pickle.load(filehandler)
                        pduModel = str(pdu.getNameplate().model)
                        if pduModel == oldPduModel :
                            initialCurrents = pickle.load(filehandler)
                            initialVoltages = pickle.load(filehandler)
                            hasNeutral = pickle.load(filehandler)
                            timeStamps = [None] * pickle.load(filehandler) # Length of the timestamps array is received from the old record.
                            print("\nOld values were loaded succesfully.")
                            break
                        else :
                            print("\nRecords are not for this device.\nThe device was: %s\nYour device: %s\nPlease start the script with new values. " %(oldPduModel, pduModel))
                            sys.exit(1)
                except IOError as io :
                    print("\nNo record exist. Please start the script with new values.")
                    sys.exit(1)
                except pickle.UnpicklingError as e:
                    print("\nRecord file corrupted. File content will be deleted.")
                    open('last-values', 'w').close()
                    sys.exit(1)
                except Exception as e:
                    print("\nAn error occured: " + e)
                    sys.exit(1)
            #If user do not want to start from the last recorded values, then initial values are generated depending on the thresholds.
            else:
                print("\nRandom values are generating...")
                initialCurrents, initialVoltages, lowerCriticalCurrents, upperCriticalVoltages, lowerCriticalVoltages, upperCriticalCurrents = [], [], [] ,[], [], []
                if poleControlled:
                    if hasMeteredOutlets and isInlineMeter:
                        for outlet in outlets:
                            poles = outlet.getIOP()[2]
                            for pole in poles:
                                if str(pole.line) != "pdumodel.PowerLine.NEUTRAL":
                                    upperCriticalCurrents.append(pole.current.getThresholds().upperCritical)
                                    lowerCriticalCurrents.append(pole.current.getThresholds().lowerCritical)
                                    upperCriticalVoltages.append(pole.voltage.getThresholds().upperCritical / math.sqrt(3))
                                    lowerCriticalVoltages.append(pole.voltage.getThresholds().lowerCritical / math.sqrt(3))
                                    initialVoltages.append(random.uniform(lowerCriticalVoltages[-1], upperCriticalVoltages[-1]))
                                    if args.thresh : # if true, then random value will be generated according to the thresholds
                                        initialCurrents.append(random.uniform(lowerCriticalCurrents[-1], upperCriticalCurrents[-1]))
                                    else: #if false, then threshold which has been calculated above, will be the upperCriticalCurrent
                                        initialCurrents.append(random.uniform(lowerCriticalCurrents[-1], upperCriticalCurrent))
                                else :
                                    hasNeutral = 1
                    else:
                        for inlet in inlets:
                            poles = inlet.getPoles()
                            for pole in poles:
                                if str(pole.line) != "pdumodel.PowerLine.NEUTRAL":
                                    upperCriticalCurrents.append(pole.current.getThresholds().upperCritical)
                                    lowerCriticalCurrents.append(pole.current.getThresholds().lowerCritical)
                                    upperCriticalVoltages.append(pole.voltage.getThresholds().upperCritical / math.sqrt(3))
                                    lowerCriticalVoltages.append(pole.voltage.getThresholds().lowerCritical / math.sqrt(3))
                                    initialVoltages.append(random.uniform(lowerCriticalVoltages[-1], upperCriticalVoltages[-1]))
                                    if args.thresh :
                                        initialCurrents.append(random.uniform(lowerCriticalCurrents[-1], upperCriticalCurrents[-1]))
                                    else:
                                        initialCurrents.append(random.uniform(lowerCriticalCurrents[-1], upperCriticalCurrent))         
                                else :
                                    hasNeutral = 1
                    timeStamps = [None] * (len(meter) * len(poles))# Time for the calculation of the active energy has also be stored.
                else :
                    for tmp in meter: 
                        upperCriticalCurrents.append(tmp.getSensors().current.getThresholds().upperCritical)
                        lowerCriticalCurrents.append(tmp.getSensors().current.getThresholds().lowerCritical)
                        upperCriticalVoltages.append(tmp.getSensors().voltage.getThresholds().upperCritical)
                        lowerCriticalVoltages.append(tmp.getSensors().voltage.getThresholds().lowerCritical)
                        initialVoltages.append(random.uniform(lowerCriticalVoltages[-1], upperCriticalVoltages[-1]))
                        if args.thresh :
                            initialCurrents.append(random.uniform(lowerCriticalCurrents[-1], upperCriticalCurrents[-1]))
                        else:
                            initialCurrents.append(random.uniform(lowerCriticalCurrents[-1], upperCriticalCurrent))       
                    timeStamps = [None] * len(meter)
                break
        
        print("\nRandomization is starting...")
        presentCurrents = [None] * len(initialCurrents)
        presentVoltages = [None] * len(initialVoltages)
        randCurrent = randVoltage = phaseAngle = activePower = apparentPower = activeEnergy = powerFactor = reactivePower = num = 0 

        #This loop sets the random values with Lua until an interrupt occur.
        print("\nPress 'CTRL + C' to stop the randomization... \n(If you want to save the values for the next time, please wait 10s before the interruption.)")
        try:
            while True: 
                for num,tmp in enumerate(meter):
                    target = meterType + "[" + str(num + 1)  + "]"
                    if poleControlled :
                        if isInlineMeter: 
                            numberOfPoles = len(tmp.getIOP()[2]) - hasNeutral
                        else :
                            numberOfPoles = len(tmp.getPoles()) - hasNeutral
                        for poleNo in range(numberOfPoles):
                            index = numberOfPoles * num + poleNo
                            calculateValues(index)
                            connectLua.run("if %s.setCurrent~=nil then %s:setCurrent(%d, %f) end" %(target, target, poleNo, randCurrent) +
                            " if %s.setVoltage~=nil then %s:setVoltage(%d, %f) end" %(target, target, poleNo, randVoltage) +
                            " if %s.setActivePower~=nil then %s:setActivePower(%d, %f) end" %(target, target, poleNo, activePower) +
                            " if %s.setApparentPower~=nil then %s:setApparentPower(%d, %f) end" %(target, target, poleNo, apparentPower) +
                            " if %s.addActiveEnergy~=nil then %s:addActiveEnergy(%d, %f) end" %(target, target, poleNo, activeEnergy) +
                            " if %s.setPowerFactor~=nil then %s:setPowerFactor(%d, %f) end" %(target, target, poleNo, powerFactor) +
                            " if %s.setReactivePower~=nil then %s:setReactivePower(%d, %f) end" %(target, target, poleNo, reactivePower))
                            timeStamps[numberOfPoles * num + poleNo] = datetime.datetime.now()
                            presentCurrents[index] = randCurrent
                            presentVoltages[index] = randVoltage
                    else :  
                        calculateValues(num) 
                        connectLua.run("if %s.setCurrent~=nil then %s:setCurrent(%f) end" %(target, target,randCurrent) + 
                        " if %s.setVoltage~=nil then %s:setVoltage(%f) end" %(target, target,randVoltage) + 
                        " if %s.setActivePower~=nil then %s:setActivePower(%f) end" %(target, target, activePower) +
                        " if %s.setApparentPower~=nil then %s:setApparentPower(%f) end" %(target, target, apparentPower) +
                        " if %s.addActiveEnergy~=nil then %s:addActiveEnergy(%f) end" %(target, target, activeEnergy) +
                        " if %s.setPowerFactor~=nil then %s:setPowerFactor(%f) end" %(target, target, powerFactor) +
                        " if %s.setReactivePower~=nil then %s:setReactivePower(%f) end" %(target, target, reactivePower))

                        timeStamps[num] = datetime.datetime.now()
                        presentCurrents[num] = randCurrent
                        presentVoltages[num] = randVoltage
                if args.slow is not None :
                    time.sleep(args.slow)
        except KeyboardInterrupt:
            if None in presentCurrents or None in presentVoltages :
                print("\nERROR: The operation was interrupted when new random values were generating. Values cannot be saved.")
                open('last-values', 'w').close()
            else:
                try:
                    pduModel = str(pdu.getNameplate().model)
                    timeStampsLength = len(timeStamps)
                    with open("last-values","wb") as filehandler:
                        pickle.dump(pduModel, filehandler)
                        pickle.dump(presentCurrents,filehandler)
                        pickle.dump(presentVoltages,filehandler)
                        pickle.dump(hasNeutral,filehandler)
                        pickle.dump(timeStampsLength,filehandler)
                        print("\nProcess stopped successfully and last values saved.")
                except Exception as e :
                    print(e)
    except Exception as e:
        if args.debug:
            traceback.print_exc()
        print("\nAn error occured. Details can be found in the log file.")
        logging.exception("Detailed info:")
        logging.debug(str(pduMetaData) + "\n-------------------------------------------------------------------------------\n")
except Exception as e :
    print ("Error: " + str(e)) 

