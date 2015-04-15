width = 8

def parse(line):
    start = line.find('[')
    end = line.find(']')
    if start == -1 or end == -1:
        return None
    return convert(line[start+1:end])

def convert(line):
    global width
    args = line.split(',')
    for i in range(0,len(args)):
        args[i] = args[i].strip()
        k = len(args[i])-1
        while args[i][k] == '0' or args[i][k] == '.':
            k -= 1
            if args[i][k+1] == '.':
                break
        args[i] = args[i][:k+1]
        args[i] = str(args[i]) + 'f'
        args[i] += ' '*(width - len(args[i]))
    
    return '\t'.join(args)
    
    
sb = []
s = input()
while s != None:
    p = parse(s)
    if p != None:
        sb.append(p)
    try:
        s = input()
    except:
        s = None
        
print(',\n'.join(sb))