package com.gii.maxflow;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by onion on 08.02.16.
 */
public class CircleNode{
    Circle node;
    Circle parent;
    CircleNode parentNode; //may have no parent, in case this node is a root node itself
    ArrayList<Operation> incomingOperations = new ArrayList<Operation>();
    ArrayList<Operation> outcomingOperations = new ArrayList<Operation>();
    ArrayList<CircleNode> children = new ArrayList<CircleNode>();
    boolean isHidden = false;
    boolean showChildren = true;
    public CircleNode(Circle currCrc){
        node = currCrc;
    }

    public void specifyNode(ArrayList<Circle> allCircles, ArrayList<CircleNode> allNodes,
                            ArrayList<Operation> operations,ArrayList<CircleNode> moneyCircleNodes){

        for(Circle crc:allCircles)
            if(node.parentId.equals(crc.id))
                parent=crc;

        for(CircleNode crcNode:allNodes)
            if(node.parentId.equals(crcNode.node.id))
                parentNode=crcNode;



        for(CircleNode circleNode:allNodes)
            if (circleNode.node.parentId.equals(this.node.id) && !children.contains(circleNode))
                children.add(circleNode);

        CircleNode meCircle = null;
        for (CircleNode crcNode : allNodes)
            if (crcNode.node.name.equals("Me"))
                meCircle = crcNode;

        for(Operation opr:operations){
            if(opr.toCircle.equals(this.node.id) && !incomingOperations.contains(opr) && !outcomingOperations.contains(opr)) {
                //following condition verifies that operation doesn't lie within the same group
                if (CircleNode.returnCircleNodeByCircleId(opr.fromCircle, allNodes) != null &&
                        CircleNode.returnCircleNodeByCircleId(opr.toCircle, allNodes) != null) {
                    CircleNode fromCircleNode = CircleNode.returnCircleNodeByCircleId(opr.fromCircle, allNodes);
                    CircleNode toCircleNode = CircleNode.returnCircleNodeByCircleId(opr.toCircle, allNodes);

                    if (!fromCircleNode.returnRootCircleNode().node.id.equals((toCircleNode).returnRootCircleNode().node.id) &&
                            fromCircleNode.node.isMyMoney() ^ toCircleNode.node.isMyMoney()) {
                        if (moneyCircleNodes != null) {
                            if (this.hasAncestor(allNodes,moneyCircleNodes) || moneyCircleNodes.contains(this)) {
                                outcomingOperations.add(opr);
                            } else
                                incomingOperations.add(opr);
                        }
                    }

                }
            }
            if (opr.fromCircle.equals(this.node.id) && !incomingOperations.contains(opr) && !outcomingOperations.contains(opr))  {
                //following condition verifies that operation doesn't lie within the same group
                if (CircleNode.returnCircleNodeByCircleId(opr.fromCircle, allNodes) != null &&
                        CircleNode.returnCircleNodeByCircleId(opr.toCircle, allNodes) != null) {
                    CircleNode fromCircleNode = CircleNode.returnCircleNodeByCircleId(opr.fromCircle, allNodes);
                    CircleNode toCircleNode = CircleNode.returnCircleNodeByCircleId(opr.toCircle, allNodes);

                    if (!fromCircleNode.returnRootCircleNode().node.id.equals((toCircleNode).returnRootCircleNode().node.id) &&
                            fromCircleNode.node.isMyMoney() ^ toCircleNode.node.isMyMoney()) {
                        if (moneyCircleNodes != null) {
                            if (this.hasAncestor(allNodes,moneyCircleNodes) || moneyCircleNodes.contains(this)) {
                                incomingOperations.add(opr);
                            } else
                                outcomingOperations.add(opr);
                        }
                    }
                }
            }
        }




        //remove child if circle has been deleted.  Here we have to use iterators
        for(Iterator<CircleNode> circleNodeIterator = children.iterator(); circleNodeIterator.hasNext();) {
            CircleNode currentCircleNode = circleNodeIterator.next();
            if(currentCircleNode.node.deleted)
                circleNodeIterator.remove();
        }

        for(Iterator<Operation> operationIterator = incomingOperations.iterator(); operationIterator.hasNext();) {
            Operation currentOperation = operationIterator.next();
            if(currentOperation.deleted)
                operationIterator.remove();
        }
        for(Iterator<Operation> operationIterator = outcomingOperations.iterator(); operationIterator.hasNext();) {
            Operation currentOperation = operationIterator.next();
            if(currentOperation.deleted)
                operationIterator.remove();
        }

        // if current node is a money node we should interchange incoming and outcoming operations
        if(this.node.isMyMoney()) {
            ArrayList<Operation> temp = incomingOperations;
            incomingOperations = outcomingOperations;
            outcomingOperations = temp;
        }
    }

    public boolean hasAncestor(Circle anc, ArrayList<Circle> allCircles){
        Circle currCrc=this.node;
        while (!currCrc.parentId.equals("")) {
            for(Circle crc: allCircles){
                if (currCrc.parentId.equals(anc.id))
                    return true;
                if(crc.id.equals(currCrc.parentId))
                    currCrc = crc;

            }
        }
        return false;
    };


    public boolean hasAncestor(CircleNode ancNode, ArrayList<CircleNode> allCircleNodes){
        CircleNode currCrc=this;
        while (currCrc.parentNode!=null) {
            if (currCrc.parentNode.node.id.equals(ancNode.node.id))
                return true;
            currCrc=currCrc.parentNode;
        }
        return false;
    };

    public boolean hasAncestor(ArrayList<CircleNode> allCircleNodes,ArrayList<CircleNode> moneyCircleNodes){
        CircleNode currCrc=this;
        while (currCrc.parentNode!=null) {
            if (moneyCircleNodes.contains(currCrc))
                return true;
            currCrc=currCrc.parentNode;
        }
        return false;
    };

    public CircleNode returnRootCircleNode(){
        if(this.parentNode!=null)
            return parentNode.returnRootCircleNode();
        else
            return this;
    }

    public static CircleNode returnCircleNodeByCircleId(String Id,ArrayList<CircleNode> allNodes){
        for(CircleNode crcNode:allNodes)
            if(crcNode.node.id.equals(Id))
                return crcNode;
        return null;
    }

    public float getValueOfIncomingOperations()
    {
        float res = 0f;
        for(Operation opr:incomingOperations)
            res=res+opr.amount;
        if(!showChildren)
            for(CircleNode child:children)
                res=res+child.getValueOfIncomingOperations();

        return res;
    }


    public float getValueOfIncomingOperations(int fromPage, int toPage)
    {
        float res = 0f;
        for(Operation opr:incomingOperations)
            if(opr.inFilter && opr.pageNo<=toPage && opr.pageNo>=fromPage)
                res=res+opr.amount;
        if(!showChildren)
            for(CircleNode child:children)
                res=res+child.getValueOfIncomingOperations(fromPage,toPage);

        return res;
    }

    public float getValueOfIncomingOperations(long fromPage, long toPage)
    {
        float res = 0f;
        for(Operation opr:incomingOperations)
            if(opr.inFilter && opr.date.getTime()<=toPage && opr.date.getTime()>=fromPage)
                res=res+opr.amount;
        if(!showChildren)
            for(CircleNode child:children)
                res=res+child.getValueOfIncomingOperations(fromPage,toPage);

        return res;
    }

    public float getValueOfOutcomingOperations()
    {
        float res = 0f;
        for(Operation opr:outcomingOperations)
            res=res+opr.amount;
        if(!showChildren)
            for(CircleNode child:children)
                res=res+child.getValueOfOutcomingOperations();

        return res;
    }


    public float getValueOfOutcomingOperations(int fromPage, int toPage)
    {
        float res = 0f;
        for(Operation opr:outcomingOperations)
            if(opr.inFilter && opr.pageNo<=toPage && opr.pageNo>=fromPage)
                res=res+opr.amount;
        if(!showChildren)
            for(CircleNode child:children)
                res=res+child.getValueOfOutcomingOperations(fromPage,toPage);

        return res;
    }


    public float getValueOfOutcomingOperations(long fromPage, long toPage)
    {
        float res = 0f;
        for(Operation opr:outcomingOperations)
            if(opr.inFilter && opr.date.getTime()<=toPage && opr.date.getTime()>=fromPage)
                res=res+opr.amount;
        if(!showChildren)
            for(CircleNode child:children)
                res=res+child.getValueOfOutcomingOperations(fromPage,toPage);

        return res;
    }

}