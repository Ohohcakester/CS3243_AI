import os

folderPath = '../src/'
#folderPath = 'C:/Users/Oh/Desktop/Programming/OhGit/EclipseProjects/CS3243_AI/src/'
mainFile = 'PlayerSkeleton.java'
excluded = ['State.java', 'TFrame.java', 'TLabel.java']

def printSafe(s):
	s = str(s).encode('ascii', 'ignore')
	s = s.decode('ascii')
	print(s)

def getName(file):
    return file[file.rfind('/')+1:]
    
def isMain(file):
    global mainFile
    return getName(file) == mainFile

def notExcluded(file):
    global excluded
    return (file not in excluded)
    
def getFirstClassInterface(s):
    firstClass = s.find('class')
    firstInterface = s.find('interface')
    if firstClass == -1:
        index = firstInterface
    elif firstInterface == -1:
        index = firstClass
    else:
        index = min(firstClass, firstInterface)
    return index
    
    
def getImports(file):
    f = open(file)
    s = f.read()
    f.close()
    
    s = s[:getFirstClassInterface(s)]
    lines = s.split('\n')
    impts = []
    for line in lines:
        if line.find('import java') != -1: 
            impts.append(line)
    return impts

def processFile(file, isPublic):
    f = open(file)
    s = f.read()
    f.close()
    
    startIndex = getFirstClassInterface(s)
    classCommentIndex = s.find('/*')
    commentEndIndex = s.find('*/', classCommentIndex)
    if classCommentIndex < startIndex < commentEndIndex:
        startIndex = commentEndIndex + getFirstClassInterface(s[commentEndIndex:])
    
    publicString = ''
    if isPublic:
        publicString = 'public '
        
    if classCommentIndex > startIndex:
        print('Missing class comment for ' + file)
        return publicString + s[startIndex:]
    else:
        return s[classCommentIndex:commentEndIndex] + '*/\n' + publicString + s[startIndex:]

    #return s[getFirstClassInterface(s):]

if __name__ == '__main__':
    ds = os.listdir(folderPath)
    allFiles = []
    for d in ds:
        files = os.listdir(folderPath + d)
        files = filter(lambda s : s[-5:] == '.java', files)
        files = filter(notExcluded, files)
        files = map(lambda s : folderPath + d + '/' + s, files)
        allFiles += files
        
    
    jImports = set()
    for file in allFiles:
        jImports = jImports.union(getImports(file))
    
    s = []
    for imp in jImports:
        s.append(imp + '\n')
    
    s.append('\n\n')
    for file in allFiles:
        if isMain(file):
            s.append(processFile(file, True))
    
    s.append('\n\n')
    for file in allFiles:
        if not isMain(file):
            pro = processFile(file, False)
            s.append(pro)
            print('- combine ' + getName(file))
            s.append('\n\n')
    
    s = ''.join(s)
    
    f = open(mainFile, 'w+')
    f.write(s)
    f.close()
    #printSafe(s)
    print ('Combination complete')
    #scenes = filter(lambda s : s[-6:] == '.unity', scenes)

    #for scene in scenes:
    #    findanimators(folderPath + scene)
