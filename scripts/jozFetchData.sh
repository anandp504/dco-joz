#!/bin/sh

# CMA is only for development use. 
function getCmaData() {
	rm -Rf qac tmc storebuilder cma.zip
	mkdir tmc
	mkdir storebuilder

	scp -o StrictHostKeyChecking=false tmc01-us.dc1.tumri.net:/usr/share/tomcat5/tmc-to-soz/t-specs.lisp tmc/
	scp -o StrictHostKeyChecking=false tmc01-us.dc1.tumri.net:/usr/share/tomcat5/tmc-to-soz/mapping.lisp tmc/
	scp -o StrictHostKeyChecking=false pub-us.dc1.tumri.net:/usr/share/tomcat5/to-soz/t-specs.lisp storebuilder/
	scp -o StrictHostKeyChecking=false pub-us.dc1.tumri.net:/usr/share/tomcat5/to-soz/mapping.lisp storebuilder/

	zip -r cma.zip tmc storebuilder

}

# CMA is only for development use.
function performCma() {
	rm -Rf /tmp/cma
	mkdir /tmp/cma
	(cd /tmp/cma; getCmaData)
	mv /tmp/cma/cma.zip .
	rm -Rf /tmp/cma
}


function getFileFromFtp() {
    if [ -a /tmp/ftp.txt ]
    then
            rm /tmp/ftp.txt
    fi;
    touch /tmp/ftp.txt
    echo quote user logs >> /tmp/ftp.txt
    echo quote pass WrN7PtB! >> /tmp/ftp.txt
    echo cd ${1} >> /tmp/ftp.txt
    echo mget ${1}*.utf8 >> /tmp/ftp.txt
    echo mget ${1}*.taxonomy >> /tmp/ftp.txt
    echo mget ${1}*.txt >> /tmp/ftp.txt
    echo bye >> /tmp/ftp.txt
    if [ ${2} == "US" ]
    then 
        ftp -in michaelbrown < /tmp/ftp.txt
    elif [ ${2} == "UK" ]
    then
        ftp -in robertnovak < /tmp/ftp.txt
    else
        echo Invalid region specified. Has to be US or UK.
        exit 1;
    fi;
    rm /tmp/ftp.txt

}


function generateDeployScript() {
	if [ $# -ne 1 ] 
	then
        echo Usage: $0 serial_num;
        exit 1;
	fi;
	
	touch deployJozData_$1.sh
	chmod a+x deployJozData_$1.sh
	
	prog='$0'
	counts='$#'
	arg1='$1'
	arg2='$2'
	outputDirVar='$outputDir'
	symlinkVar='$symlink'
	sudoVar='$sudoReq'
	retValVar='$retVal'
	
	echo "#!/bin/sh 
#
# See if someone's looking for help
	if ( [ $counts -ge 1 ] && ( [ $arg1 == "usage" ] || [ $arg1 == "-h" ] || [ $arg1 == "help" ] ))
	then
		echo Usage: $prog [output_dir] [symlink name]
		exit 1;
	fi;

	# Check if a parameter is passed in.
	# The expected parameters are 
	if [ $counts -ge 1 ]
	then
		outputDir=$arg1
	else
		outputDir=\"/opt/joz/data/caa\"
	fi;

	if [ $counts -ge 2 ]
	then
		symlink=$arg2
	else
		symlink=\"current\"
	fi;
	
	if [ -d $outputDirVar/$1 ]
	then
		echo $outputDirVar/$1 exists. Delete it before running this script.
		exit 1;
	fi;
		
	# If the directory doesn't exist, create it.
	if [ ! -d $outputDirVar ] 
	then
		mkdir -p $outputDirVar
		if [ ! -d $outputDirVar ]
		then
			# If we cannot create the directory, try with sudo.
			sudo mkdir -p $outputDirVar
			if [ ! -d $outputDirVar ]
			then
				echo Cannot create directory $outputDirVar
				exit 1
			fi;
		fi;
	fi;
	
	# If Symlink exists and if the directory or symlink is not writable, we will need sudo permission.
	if [ -L $outputDirVar/$symlinkVar ]
	then
		if ( [ ! -w $outputDirVar ] || [ ! -w $outputDirVar/$symlinkVar ] )
		then
			sudoReq="sudo"
		else
			sudoReq=""
		fi;
	elif [ ! -w $outputDirVar ]
	then
		sudoReq="sudo"
	fi;
		
	
	# Unzip the data package
	unzip jozData_$1.zip
	
	# Move it to appropriate directory
	$sudoVar mv $1 $outputDirVar
		
	# Change the symlink
	if [ -L $outputDirVar/$symlinkVar ]
	then
		$sudoVar rm $outputDirVar/$symlinkVar
	fi;
	$sudoVar ln -sfn $outputDirVar/$1 $outputDirVar/$symlinkVar		
" >> deployJozData_$1.sh
	
}

function getCaaData() {

	if [ $# -ne 2 ] 
	then
        echo Usage: $0 region serial_num;
        exit 1;
	fi;
	
	if [ -a $2.tgz ] 
	then
		rm -f $2.tgz
	fi;
	
	if [ -d $2 ]
	then
		rm -Rf $2
	fi;

	region=$1;
	serial_num=$2;

	echo Downloading MUP for region ${region} serial number ${serial_num};

	mkdir $2
	mkdir $2/data 
	(cd $2/data; getFileFromFtp ${serial_num} ${region})

	# Create Lucene Index
	curr_dir=`pwd`;
	(cd /tmp/IndexCreator; ./createIndex.sh -docDir ${curr_dir}/$2/data -indexDir ${curr_dir}/$2/lucene)
	
	zip -r jozData_$2.zip $2
	(cd /tmp/caa; generateDeployScript $2)
	
	tar czf $2.tgz jozData_$2.zip deployJozData_$2.sh 

}


function performCaa() {

	if [ $# -ne 2 ] 
	then
		echo Two arguments need to be supplied
        echo Usage: $0 region serial_num;
        exit 1;
	fi;

	curr_dir=`pwd`

	if [[ ! -a ${curr_dir}/IndexCreator.tgz ]]
	then
		echo "Error: Need IndexCreator.tgz to create lucene index"
		exit 1;
	fi; 

	rm -Rf /tmp/caa
	rm -Rf /tmp/IndexCreator

	mkdir /tmp/caa
	mkdir /tmp/IndexCreator

	tar xzf IndexCreator.tgz -C /tmp/IndexCreator/ 

	(cd /tmp/caa; getCaaData $@)

	mv /tmp/caa/$2.tgz .

	rm -Rf /tmp/caa
	rm -Rf /tmp/IndexCreator
}

function perform() {

	progName=$0

	if ( [ $# -eq 3 ] || [ $# -eq 1 ] )
	then
		module=$1;
		shift
	else
		module="caa";
	fi;
	
	if [ "$module" == "both" ]
	then 
		performCaa $@
		performCma
	elif [ "$module" == "caa" ]
	then
		performCaa $@
	elif [ "$module" == "cma" ]
	then
		performCma
	else
		echo Usage: progName [caa|cma|both] region serial_num
	fi;

}

perform $@



echo Done.
